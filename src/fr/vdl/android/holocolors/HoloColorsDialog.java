/*
 * Copyright 2000-2013 JetBrains s.r.o.
 * Copyright 2013-2014 Android Holo Colors (Jerome Van Der Linden)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.vdl.android.holocolors;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.ColorChooser;
import com.intellij.ui.ColorUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author jva
 */
public class HoloColorsDialog extends DialogWrapper {

  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
  public static final String IDEA_FOLDER = "/.idea";

  private Project project;
  private JComboBox themeComboBox;
  private TextFieldWithBrowseButton colorTextField;
  private TextFieldWithBrowseButton resPathTextField;
  private JCheckBox editTextCheckBox;
  private JCheckBox autocompleteCheckBox;
  private JCheckBox buttonCheckBox;
  private JCheckBox coloredButtonCheckBox;
  private JCheckBox checkBoxCheckBox;
  private JCheckBox radioCheckBox;
  private JCheckBox spinnerCheckBox;
  private JCheckBox coloredSpinnerCheckBox;
  private JCheckBox progressBarCheckBox;
  private JCheckBox seekBarCheckBox;
  private JCheckBox ratingBarCheckBox;
  private JCheckBox ratingBarSmallCheckBox;
  private JCheckBox ratingBarBigCheckBox;
  private JCheckBox toggleCheckBox;
  private JCheckBox listSelectorCheckBox;
  private JCheckBox fastScrollCheckBox;
  private JCheckBox switchCheckBox;
  private JPanel ahcPanel;
  private JTextField nameTextField;
  private JCheckBox kitkatCheckBox;
  private JCheckBox textHandleCheckBox;
  private JRadioButton oldSdkRadio;
  private JComboBox compatComboBox;
  private JRadioButton holoSdkRadio;
  private JLabel compatLabel;
  private JLabel sdkLabel;
  private JLabel resFolderLabel;
  private JLabel nameLabel;
  private JLabel colorLabel;
  private JLabel themeLabel;
  private JDialog loadingDialog;

  /**
   * @param project
   */
  public HoloColorsDialog(@Nullable final Project project) {
    super(project, true);

    loadingDialog = new JDialog(getWindow(), "Downloading, please wait...", Dialog.ModalityType.MODELESS);
    loadingDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    loadingDialog.setSize(300, 20);
    loadingDialog.setLocationRelativeTo(ahcPanel);

    this.project = project;

    checkLicence();

    setTitle("Android Holo Colors Generator");
    setResizable(true);

    FileChooserDescriptor workingDirectoryChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
    String title = "Select res directory";
    workingDirectoryChooserDescriptor.setTitle(title);
    resPathTextField.addBrowseFolderListener(title, null, project, workingDirectoryChooserDescriptor);

    colorTextField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Color color = ColorChooser.chooseColor(WindowManager.getInstance().suggestParentWindow(project), "Select color", null);
        if (color != null) {
          colorTextField.setText('#' + ColorUtil.toHex(color));
          colorTextField.setForeground(color);
        }
      }
    });

    // select appcompat by default
    compatComboBox.setSelectedIndex(2);
    oldSdkRadio.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        compatComboBox.setEnabled(oldSdkRadio.isSelected());
      }
    });

    init();
  }

  @Override
  protected void doOKAction() {
    String color = colorTextField.getText();

    String themeSelected = (String)themeComboBox.getSelectedItem();
    if ("Light".equals(themeSelected)) {
      themeSelected = "light";
    }
    else if ("Dark".equals(themeSelected)) {
      themeSelected = "dark";
    }
    else {
      themeSelected = "light_dark_action_bar";
    }

    String compatSelected = (String)compatComboBox.getSelectedItem();
    if ("None".equals(compatSelected)) {
      compatSelected = "old";
    }
    else if ("Sherlock".equals(compatSelected)) {
      compatSelected = "abs";
    }
    else {
      compatSelected = "compat";
    }

    int kitkat = 0;
    if (kitkatCheckBox.isSelected()) {
      kitkat = 1;
    }

    String urlZip = "http://android-holo-colors.com/generate_all.php?color=" +
                    color.replaceFirst("#", "") +
                    "&holo=" +
                    themeSelected +
                    "&name=" +
                    nameTextField.getText().replaceAll(" ", "") +
                    "&kitkat=" + kitkat +
                    "&minsdk=" + (oldSdkRadio.isSelected() ? "old" : "holo") +
                    "&compat=" + compatSelected;

    if (editTextCheckBox.isSelected()) {
      urlZip += "&edittext=true";
    }
    if (textHandleCheckBox.isSelected()) {
      urlZip += "&text_handle=true";
    }
    if (autocompleteCheckBox.isSelected()) {
      urlZip += "&autocomplete=true";
    }
    if (buttonCheckBox.isSelected()) {
      urlZip += "&button=true";
    }
    if (coloredButtonCheckBox.isSelected()) {
      urlZip += "&cbutton=true";
    }
    if (checkBoxCheckBox.isSelected()) {
      urlZip += "&checkbox=true";
    }
    if (radioCheckBox.isSelected()) {
      urlZip += "&radio=true";
    }
    if (spinnerCheckBox.isSelected()) {
      urlZip += "&spinner=true";
    }
    if (coloredSpinnerCheckBox.isSelected()) {
      urlZip += "&cspinner=true";
    }
    if (progressBarCheckBox.isSelected()) {
      urlZip += "&progressbar=true";
    }
    if (seekBarCheckBox.isSelected()) {
      urlZip += "&seekbar=true";
    }
    if (toggleCheckBox.isSelected()) {
      urlZip += "&toggle=true";
    }
    if (listSelectorCheckBox.isSelected()) {
      urlZip += "&list=true";
    }
    if (ratingBarCheckBox.isSelected()) {
      urlZip += "&ratingbar=true";
    }
    if (ratingBarSmallCheckBox.isSelected()) {
      urlZip += "&ratingstarsmall=true";
    }
    if (ratingBarBigCheckBox.isSelected()) {
      urlZip += "&ratingstarbig=true";
    }
    if (fastScrollCheckBox.isSelected()) {
      urlZip += "&fastscroll=true";
    }
    if (switchCheckBox.isSelected()) {
      urlZip += "&switchjb=true";
    }

    try {
      loadingDialog.setVisible(true);
      File zipFile = downloadZip(urlZip);
      loadingDialog.dispose();

      unzipFile(zipFile);
      zipFile.delete();
    }
    catch (Exception e) {
      JOptionPane.showMessageDialog(ahcPanel, "Unable to generate your theme, please go to http://android-holo-colors.com");
    }

    super.doOKAction();
  }

  @Nullable
  @Override
  protected ValidationInfo doValidate() {
    if (StringUtils.isEmpty(nameTextField.getText().trim())) {
      return new ValidationInfo("Please select a name for your theme.", nameTextField);
    }

    if (StringUtils.isEmpty(resPathTextField.getText().trim())) {
      return new ValidationInfo("Please select res folder in order to unzip the holo colors archive.", resPathTextField);
    }

    if (StringUtils.isEmpty(colorTextField.getText().trim())) {
      return new ValidationInfo("Please select a valid color.", colorTextField);
    }

    return null;
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return ahcPanel;
  }


  // ----------------------------------
  // PRIVATE
  // ----------------------------------

  private File downloadZip(String urlZip) throws Exception {
    new File(project.getBaseDir().getPath() + IDEA_FOLDER).mkdirs();
    File zipFile = new File(project.getBaseDir().getPath() + IDEA_FOLDER, "AndroidHoloColors_" + nameTextField.getText() + ".zip");

    InputStream is = null;
    FileOutputStream os = null;
    try {
      URL url = new URL(urlZip);
      is = url.openStream();

      os = new FileOutputStream(zipFile);
      byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
      int n;
      while (-1 != (n = is.read(buffer))) {
        os.write(buffer, 0, n);
      }
    }
    finally {
      if (is != null) {
        try {
          is.close();
        }
        catch (IOException e) {
        }
      }
      if (os != null) {
        try {
          os.close();
        }
        catch (IOException e) {
        }
      }
    }

    return zipFile;
  }

  private void unzipFile(File zipFile) throws Exception {
    File outputFolder = new File(resPathTextField.getText());

    boolean overwriteAll = false;
    boolean overwriteNone = false;
    Object[] overwriteOptions = {"Overwrite this file", "Overwrite all", "Do not overwrite this file", "Do not overwrite any file"};

    ZipInputStream zis = null;
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    try {
      zis = new ZipInputStream(new FileInputStream(zipFile));
      ZipEntry ze = zis.getNextEntry();
      while (ze != null) {
        String fileName = ze.getName().replaceFirst("res/", "");
        File newFile = new File(outputFolder + File.separator + fileName);

        new File(newFile.getParent()).mkdirs();

        boolean overwrite = overwriteAll || (!newFile.exists());
        if (newFile.exists() && newFile.isFile() && !overwriteAll && !overwriteNone) {
          int option = JOptionPane
            .showOptionDialog(ahcPanel, newFile.getName() + " already exists, overwrite ?", "File exists", JOptionPane.YES_NO_CANCEL_OPTION,
                              JOptionPane.QUESTION_MESSAGE, new ImageIcon(getClass().getResource("/icons/H64.png")), overwriteOptions, overwriteOptions[0]);

          switch (option) {
            case 0:
              overwrite = true;
              break;
            case 1:
              overwrite = true;
              overwriteAll = true;
              break;
            case 2:
              overwrite = false;
              break;
            case 3:
              overwrite = false;
              overwriteNone = true;
              break;
            default:
              overwrite = false;
          }
        }

        if (overwrite && !fileName.endsWith(File.separator)) {
          FileOutputStream fos = new FileOutputStream(newFile);
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
          fos.close();
        }
        ze = zis.getNextEntry();
      }

      zis.closeEntry();
      zis.close();
    }
    finally {
      if (zis != null) {
        try {
          zis.close();
        }
        catch (IOException e) {
        }
      }
    }
  }

  private void checkLicence() {
    try {
      String userHome = System.getProperty("user.home");
      File holoColorsFolder = new File(userHome + File.separator + ".holocolors");
      File licenceFile = new File(holoColorsFolder, ".licence");
      File noDonationFile = new File(holoColorsFolder, ".nodonation");

      if (noDonationFile.exists()) {
        return;
      }

      int usage = 1;
      boolean showPopup = false;
      if (!holoColorsFolder.exists()) {
        holoColorsFolder.mkdir();
        showPopup = true;
        licenceFile.createNewFile();
      }
      else {
        Scanner in = new Scanner(new FileReader(licenceFile));
        if (in.hasNextInt()) {
          usage = in.nextInt() + 1;
        }
        in.close();
      }
      if (usage > 10) {
        usage = 1;
        showPopup = true;
      }
      Writer out = new BufferedWriter(new FileWriter(licenceFile));
      out.write(String.valueOf(usage));
      out.close();

      if (showPopup) {
        Object[] donationOption = {"Make a donation", "Maybe later", "No Never"};
        int option = JOptionPane.showOptionDialog(ahcPanel,
                                                  "Thanks for using Android Holo Colors!\n\nAndroid Holo Colors (website and plugin) is free to use.\nIf you save time and money with it, please make a donation.",
                                                  "Support Android Holo Colors", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, new ImageIcon(getClass().getResource("/icons/H64.png")),
                                                  donationOption, donationOption[0]);
        if (option == 0) {
          openWebpage("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XQSBX55A2Z46U");
        }
        if (option == 2) {
          noDonationFile.createNewFile();
        }
      }
    }
    catch (Exception e) {
      // no matter, nothing to do
      e.printStackTrace();
    }
  }

  private void openWebpage(String url) {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URL(url).toURI());
      }
      catch (Exception e) {
        // no matter, nothing to do
        e.printStackTrace();
      }
    }
  }
}
